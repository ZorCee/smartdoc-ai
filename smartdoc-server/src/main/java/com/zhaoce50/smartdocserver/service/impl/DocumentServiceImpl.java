package com.zhaoce50.smartdocserver.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoce50.smartdocserver.entity.Document;
import com.zhaoce50.smartdocserver.entity.KnowledgeBase;
import com.zhaoce50.smartdocserver.mapper.DocumentChunkMapper;
import com.zhaoce50.smartdocserver.mapper.DocumentMapper;
import com.zhaoce50.smartdocserver.service.DocumentService;
import com.zhaoce50.smartdocserver.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import com.zhaoce50.smartdocserver.entity.DocumentChunk;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    //私有安检员：校验知识库是否存在且属于当前登录用户
    private void checkKbOwnership(Long kbId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        KnowledgeBase kb = knowledgeBaseService.getById(kbId);
        if (kb == null || !kb.getOwnerId().equals(currentUserId)){
            throw new RuntimeException("知识库不存在或无权限操作！");
        }
    }
    @Autowired
    private DocumentChunkMapper documentChunkMapper;
    //私有切片器：按自然段落切分文档并入库
    private void saveDocumentChunks(Document doc, String content){
        String[] paragraphs = content.split("\\r?\\n\\s*\\r?\\n");
        int index = 0;
        for (String para : paragraphs){
            if (!para.isBlank()) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setKbId(doc.getKbId());
                chunk.setDocId(doc.getId());
                chunk.setChunkIndex(index++);
                chunk.setContent(para.trim());
                chunk.setCharCount(para.trim().length());
                documentChunkMapper.insert(chunk);
            }
        }
    }

    // 多格式文本提取引擎：支持 PDF、Word(.docx)、TXT、Markdown
    private String extractText(File file, String fileType) throws IOException {
        if ("pdf".equals(fileType)) {
            try (PDDocument pdf = PDDocument.load(file)) {
                return new PDFTextStripper().getText(pdf);
            }
        } else if ("docx".equals(fileType)) {
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument docx = new XWPFDocument(fis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
                return extractor.getText();
            }
        } else {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public Document uploadDocument(Long kbId, MultipartFile file){
        //1.校验知识库归属权与文件非空
        checkKbOwnership(kbId);
        if (file == null || file.isEmpty()){
            throw new RuntimeException("上传的文件不能为空！");
        }
        //2.提取原始文件名与后缀类型（如txt,md,pdf)
        String originalName = file.getOriginalFilename();
        String fileType = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        //3.准备本地磁盘存储目录与唯一物理文件名
        File uploadDir = new File("D:/smartdoc-ai/uploads/");
        if (!uploadDir.exists()){
            uploadDir.mkdirs();
        }
        File destFile = new File(uploadDir, UUID.randomUUID() + "." + fileType);
        //4.写入磁盘并读取文本字数、保存数据库记录
        try {
            file.transferTo(destFile);
            String content = extractText(destFile, fileType);

            Document doc = new Document();
            doc.setKbId(kbId);
            doc.setName(originalName);
            doc.setFilePath(destFile.getAbsolutePath());
            doc.setFileSize(file.getSize());
            doc.setFileType(fileType);
            doc.setWordCount(content.length());
            doc.setStatus(3); //3-已就绪
            save(doc);
            // 5. 自动触发智能文本切片入库
            saveDocumentChunks(doc, content);
            return doc;
        } catch (IOException e){
            throw new RuntimeException("文件上传或解析失败：" + e.getMessage());
        }
    }
    @Override
    public List<Document> listByKbId(Long kbId){
        //1.校验该知识库是否属于当前用户
        checkKbOwnership(kbId);
        //2.查询该知识库下的所有文档，按上传时间倒序
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getKbId, kbId).orderByDesc(Document::getCreateTime);
        return list(wrapper);
    }
    @Override
    public void deleteDocument(Long id) {
        //1.先查出文档是否存在
        Document doc = getById(id);
        if (doc == null) {
            throw new RuntimeException("文档不存在！");
        }
        //2.校验该文档所属的知识库是否归当前用户所有
        checkKbOwnership(doc.getKbId());
        //3.执行逻辑删除
        removeById(id);
    }
    @Override
    public List<DocumentChunk> listChunksByDocId(Long docId) {
        Document doc = getById(docId);
        if (doc == null) {
            throw new RuntimeException("文档不存在！");
        }
        checkKbOwnership(doc.getKbId());
        LambdaQueryWrapper<DocumentChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentChunk::getDocId, docId)
                .orderByAsc(DocumentChunk::getChunkIndex);
        return documentChunkMapper.selectList(wrapper);
    }

    @Override
    public List<DocumentChunk> searchChunks(Long kbId, String keyword) {
        checkKbOwnership(kbId);
        LambdaQueryWrapper<DocumentChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentChunk::getKbId, kbId)
                .like(DocumentChunk::getContent, keyword)
                .last("LIMIT 5");
        return documentChunkMapper.selectList(wrapper);
    }
}
