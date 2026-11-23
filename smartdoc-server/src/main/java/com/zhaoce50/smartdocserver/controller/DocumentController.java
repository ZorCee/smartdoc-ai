package com.zhaoce50.smartdocserver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zhaoce50.smartdocserver.common.Result;
import com.zhaoce50.smartdocserver.entity.Document;
import com.zhaoce50.smartdocserver.entity.DocumentChunk;
import com.zhaoce50.smartdocserver.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/doc")
@SaCheckLogin
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    //1.上传文档接口
    @PostMapping("/upload")
    public Result<Document> upload(@RequestParam("kbId") Long kbId, @RequestParam("file")MultipartFile file){
        Document doc = documentService.uploadDocument(kbId, file);
        return Result.success(doc);
    }
    //2.查询指定知识库下的文档列表
    @GetMapping("/list/{kbId}")
    public Result<List<Document>> list(@PathVariable  Long kbId) {
        List<Document> list = documentService.listByKbId(kbId);
        return Result.success(list);
    }
    //3.删除指定文档
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success("文档删除成功！");
    }
    //4.查看指定文档的所有切片卡片
    @GetMapping("/chunks/{docId}")
    public Result<List<DocumentChunk>> listChunks(@PathVariable Long docId) {
        return Result.success(documentService.listChunksByDocId(docId));
    }
    //5.RAG检索测试接口：在指定知识库中搜索匹配的知识切片
    @GetMapping("/search")
    public Result<List<DocumentChunk>> search(@RequestParam("kbId") Long kbId,
                                              @RequestParam("keyword") String keyword){
        return Result.success(documentService.searchChunks(kbId,keyword));
    }
}
