/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.entity;

public class AggregationConfig {
    /**
     * 聚合公式
     */
    private Aggregation aggregation;
    /**
     *聚合结果保存属性 如果不指定则以 factor_agg命名 如 age_avg
     */
    private String alias;
    /**
     * 计算因子属性 取条目中的factor属性的值参与计算
     */
    private String field;
    /**
     *精度(小数位)
     */
    private int scale;
    /**
     * 舍入模式 参考BigDecimal静态常量
     * ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     * ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1, 即截短). 如:2.36 转成 2.3<br/>
     * ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正, 则舍入行为与 ROUND_UP 相同 如果为负, 则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     * ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正, 则舍入行为与 ROUND_DOWN 相同 如果为负, 则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     * ROUND_HALF_UP   = 4 四舍五入<br/>
     * ROUND_HALF_DOWN = 5 五舍六入<br/>
     * ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *   如果舍弃部分左边的数字为奇数, 则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *   如果为偶数, 则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *   如:1.15 转成 1.2, 因为5前面的1是奇数;1.25 转成 1.2, 因为5前面的2是偶数<br/>
     *      *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     */
    private int round;
    public AggregationConfig() {}
    public AggregationConfig(Aggregation aggregation, String factor, String field){
        this.aggregation = aggregation;
        this.field = factor;
        this.alias = field;
    }
    public AggregationConfig(Aggregation aggregation, String factor){
        this.aggregation = aggregation;
        this.field = factor;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }
}
