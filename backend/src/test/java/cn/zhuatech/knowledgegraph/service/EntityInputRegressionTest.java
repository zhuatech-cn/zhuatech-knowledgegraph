/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
package cn.zhuatech.knowledgegraph.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
class EntityInputRegressionTest {
  private final EntityResolutionService service = new EntityResolutionService();

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void missingOptionalIdentifiersDoNotCrash() {
    var r =
        service.resolve(
            new EntityResolutionService.Request(
                "L",
                "R",
                "SYSTEM",
                "SYSTEM",
                null,
                null,
                new BigDecimal("0.9"),
                2,
                false,
                90,
                false));
    assertEquals(EntityResolutionService.Decision.REVIEW, r.decision());
    assertEquals(48, r.resolutionScore());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void blankIdentifiersDoNotAddAuthorityScore() {
    var r =
        service.resolve(
            new EntityResolutionService.Request(
                "L", "R", "SYSTEM", "SYSTEM", "", "", new BigDecimal("0.9"), 2, false, 90, false));
    assertEquals(48, r.resolutionScore());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void missingSimilarityHasExplicitValidation() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.resolve(
                new EntityResolutionService.Request(
                    "L", "R", "SYSTEM", "SYSTEM", null, null, null, 2, false, 90, false)));
  }
}
