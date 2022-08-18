package org.anyline.entity.operator;

public class CompareBuilder {

    //根据情况自动识别
    public static Compare AUTO       = new Auto();
    public static Compare EQUAL      = new Equal();
    public static Compare BETWEEN    = new Between();
    public static Compare BIG        = new Big();
    public static Compare BIG_EQUAL  = new BigEqual();
    public static Compare END_WITH   = new EndWith();
    public static Compare START_WITH = new StartWith();
    public static Compare IN         = new In();
    public static Compare LESS       = new Less();
    public static Compare LESS_EQUAL = new LessEqual();
    public static Compare LIKE       = new Like();
}
