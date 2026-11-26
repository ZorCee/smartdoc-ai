package com.zorcee.smartdocserver.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zorcee.smartdocserver.dto.CreateKBDTO;
import com.zorcee.smartdocserver.entity.KnowledgeBase;
import com.zorcee.smartdocserver.mapper.KnowledgeBaseMapper;
import com.zorcee.smartdocserver.service.KnowledgeBaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
implements KnowledgeBaseService {
    @Override
    public Long createKnowledgeBase(CreateKBDTO dto){
        //1.从当前绘画安全提取用户ID（防止伪造ownerId）
        Long currentUserId = StpUtil.getLoginIdAsLong();
        //2.将DTO的属性一键复印到实体对象中
        KnowledgeBase kb = new KnowledgeBase();
        BeanUtils.copyProperties(dto,kb);
        kb.setOwnerId(currentUserId);
        //3.入库保存（Mybatis-plus会自动将数据库自增的id回填到kb中）
        save(kb);
        return kb.getId();
    }
    @Override
    public List<KnowledgeBase> listMyKnowledgeBases(){
        //1.获取当前登录用户id
        Long currentUserId = StpUtil.getLoginIdAsLong();
        //2.构造查询条件：只查属于当前用户的，并按创建时间倒序排列
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getOwnerId, currentUserId)
                .orderByDesc(KnowledgeBase::getCreateTime);
        //3.执行查询并返回
        return list(wrapper);
    }
    @Override
    public void deleteKnowledgeBase(Long id){
        //1.获取当前登录人ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        //2.查出该知识库
        KnowledgeBase kb = getById(id);
        if (kb == null){
            throw new RuntimeException("知识库不存在！");
        }
        //3.防越权校验：必须是创建者本人才能删除
        if (!kb.getOwnerId().equals(currentUserId)){
            throw new RuntimeException("无权删除他人的知识库！");
        }
        //4.执行逻辑删除（底层自动执行UPDATE IS_DELETED = 1）
        removeById(id);
    }
}
