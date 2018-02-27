package com.bpplus;


public class TaskRelation {
    public static final Integer DIRECTCAUSAL = 1; //-->
    public static final Integer INVERSEDIRECTCAUSAL = 2;  //<--
    public static final Integer INDIRECTCAUSAL = 4;    //->>
    public static final Integer INVERSEINDIRECTCAUSAL = 5; //<<-
    public static final Integer IMPLICTCAUSAL = 8; // 1->
    public static final Integer INVERSEIMPLICTCAUSUAL = 9; // <-1
    public static final Integer CONCURRENCY = 16;    //||
    public static final Integer CONFLICT = 32; //#
    public static final Integer L2LOOP = 64; // <>
    
    public static final String SLIENTTRANSITION = "inv_";

}