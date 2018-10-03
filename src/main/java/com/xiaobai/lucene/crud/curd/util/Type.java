package com.xiaobai.lucene.crud.curd.util;

public enum Type {

    TERM(1),PARSER,ALL,RANGE,REGEX,FUZZY,PHRASE,BOOLEAN;

    private int value;
    Type() {}
    Type(int v) {
        this.value = v;
    }
}
