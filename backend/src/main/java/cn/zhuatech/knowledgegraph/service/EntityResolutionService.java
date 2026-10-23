/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.knowledgegraph.service;

import jakarta.validation.constraints.*;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/**
 * 根据权威标识、名称、类型、来源和关系冲突决定实体合并。
 *
 * <p>商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/
 */
@Service
public class EntityResolutionService {
  /** 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
  public Result resolve(Request r) {
    List<String> evidence = new ArrayList<>(), conflicts = new ArrayList<>();
    int score = 0;
    if (r.nameSimilarity() == null) throw new IllegalArgumentException("名称相似度不能为空");
    if (r.leftType().equalsIgnoreCase(r.rightType())) {
      score += 20;
      evidence.add("实体类型一致");
    } else conflicts.add("实体类型冲突");
    if (r.leftAuthoritativeId() != null
        && !r.leftAuthoritativeId().isBlank()
        && r.leftAuthoritativeId().equals(r.rightAuthoritativeId())) {
      score += 50;
      evidence.add("权威标识一致");
    }
    score += r.nameSimilarity().multiply(new BigDecimal("20")).intValue();
    if (r.nameSimilarity().compareTo(new BigDecimal("0.9")) >= 0) evidence.add("标准化名称高度相似");
    if (r.independentSourceCount() >= 2) {
      score += 10;
      evidence.add("至少两个独立来源支持");
    }
    if (r.relationshipConflict()) conflicts.add("关键关系存在互斥事实");
    score = Math.min(100, score);
    Decision d =
        !conflicts.isEmpty()
            ? Decision.KEEP_SEPARATE
            : score >= r.autoMergeThreshold() ? Decision.MERGE : Decision.REVIEW;
    if (d == Decision.REVIEW && r.humanApproved()) d = Decision.MERGE;
    List<String> actions =
        List.of(
            d == Decision.MERGE
                ? "保留别名、来源和旧实体重定向，生成可撤销合并记录"
                : d == Decision.REVIEW ? "进入实体管理员复核队列并展示逐项证据" : "保持独立实体并记录冲突，禁止自动传播关系");
    return new Result(d, score, List.copyOf(evidence), List.copyOf(conflicts), actions);
  }

  /** 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
  public record Request(
      @NotBlank String leftEntityId,
      @NotBlank String rightEntityId,
      @NotBlank String leftType,
      @NotBlank String rightType,
      String leftAuthoritativeId,
      String rightAuthoritativeId,
      @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal nameSimilarity,
      @Min(0) int independentSourceCount,
      boolean relationshipConflict,
      @Min(1) @Max(100) int autoMergeThreshold,
      boolean humanApproved) {}

  /** 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
  public record Result(
      Decision decision,
      int resolutionScore,
      List<String> supportingEvidence,
      List<String> conflicts,
      List<String> actions) {}

  /** 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
  public enum Decision {
    MERGE,
    REVIEW,
    KEEP_SEPARATE
  }
}
