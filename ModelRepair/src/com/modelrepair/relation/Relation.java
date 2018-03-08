package com.modelrepair.relation;

public class Relation {
	//a>b
    public static int BEFORE = 1;
    //a>>b
    public static int  REACHABLE = 2;    
    public static int  INVERSEREACHABLE = 3;
    //a^b (aba)
    public static int L2LOOP = 4;
    //a->b
    public static int DIRECTCAUSAL = 8;
    public static int INVERSEDIRECTCAUSAL = 10;
    
    //a|->b
    public static  int IMPLICTCAUSAL = 16; // 1->/a>>b
    public static  int INVERSEIMPLICTCAUSUAL = 18; // <-1
    //a||b
    public static int CONCURRENCY = 32;
    //a#b
    public static int CONFLICT = 64;
    
    //a<|b
    public static int COMMPRED = 128;
    //a|>b
    public static int COMMSUCC = 129;

    //invisible flag
    public static  String SLIENTTRANSITION = "inv_";

    //And Split
    public static int ANDSPLIT = 1;
    //And Join
    public static int ANDJOIN = 2;
    //Or Split
    public static int ORSPLIT = 4;
    //Or Join
    public static int ORJOIN = 8;
    //Or Join with Different output tasks
    public static int ORJOINDIFFOUT = 16;
    
    
}