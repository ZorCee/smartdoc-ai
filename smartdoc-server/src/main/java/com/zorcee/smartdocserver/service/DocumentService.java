package com.zorcee.smartdocserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zorcee.smartdocserver.entity.Document;
import com.zorcee.smartdocserver.entity.DocumentChunk;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService extends IService<Document> {
    //1.上传文档到指定知识库并自动解析字数
    Document uploadDocument(Long kbID, MultipartFile file);
    //2.查询指定知识库下的文档列表（带权限校验）
    List<Document> listByKbId(Long kbId);
    //3.删除指定文档（带权限校验）
    void deleteDocument(Long id);
    //4.查询指定文档切分出的所有切片列表
    List<DocumentChunk> listChunksByDocId(Long docId);
    // 5. 在指定知识库中检索匹配的文本切片 (RAG 检索核心)
    List<DocumentChunk> searchChunks(Long kbId, String keyword);
}
