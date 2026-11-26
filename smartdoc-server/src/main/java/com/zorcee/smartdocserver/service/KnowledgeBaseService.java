package com.zorcee.smartdocserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zorcee.smartdocserver.dto.CreateKBDTO;
import com.zorcee.smartdocserver.entity.KnowledgeBase;

import java.util.List;

public interface KnowledgeBaseService extends IService<KnowledgeBase> {
    //1.创建知识库（自动绑定当前登录人，返回新知识库ID）
    Long createKnowledgeBase(CreateKBDTO dto);

    //2.查询当前用户有权限看到的知识库（私有库仅自己，公开库全团队）
    List<KnowledgeBase> listMyKnowledgeBases();

    //3.删除指定知识库（严格校验数据归属权，防越权删除）
    void deleteKnowledgeBase(Long id);
}
