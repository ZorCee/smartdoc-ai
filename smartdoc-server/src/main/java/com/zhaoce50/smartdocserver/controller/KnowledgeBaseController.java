package com.zhaoce50.smartdocserver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zhaoce50.smartdocserver.common.Result;
import com.zhaoce50.smartdocserver.dto.CreateKBDTO;
import com.zhaoce50.smartdocserver.entity.KnowledgeBase;
import com.zhaoce50.smartdocserver.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
@SaCheckLogin
public class KnowledgeBaseController {
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    //1.创建知识库接口
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody CreateKBDTO dto){
        Long kbId = knowledgeBaseService.createKnowledgeBase(dto);
        return Result.success(kbId);
    }
    //2.获取知识库列表接口
    @GetMapping("/list")
    public Result<List<KnowledgeBase>> list(){
        List<KnowledgeBase> list = knowledgeBaseService.listMyKnowledgeBases();
        return Result.success(list);
    }
    //3.删除知识库接口（路径变量提取ID）
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        knowledgeBaseService.deleteKnowledgeBase(id);
        return Result.success("知识库删除成功！");
    }
}
