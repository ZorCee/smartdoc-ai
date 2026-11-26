package com.zorcee.smartdocserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kb_retrieval_test")
public class RetrievalTest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long kbId;
    private Long userId;
    private String queryText;
    private Integer topK;
    private Double scoreThreshold;
    private Integer hitCount;
    private Double maxScore;
    private Long costTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}