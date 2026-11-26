package com.zorcee.smartdocserver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zorcee.smartdocserver.annotation.OperationLog;
import com.zorcee.smartdocserver.common.Result;
import com.zorcee.smartdocserver.dto.CreateKBDTO;
import com.zorcee.smartdocserver.entity.KnowledgeBase;
import com.zorcee.smartdocserver.service.KnowledgeBaseService;
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
    @OperationLog(module = "知识库管理", action = "新增知识库")
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
    @OperationLog(module = "知识库管理", action = "删除知识库")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        knowledgeBaseService.deleteKnowledgeBase(id);
        return Result.success("知识库删除成功！");
    }
}
