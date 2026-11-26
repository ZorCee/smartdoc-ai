package com.zorcee.smartdocserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zorcee.smartdocserver.entity.Document;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}
