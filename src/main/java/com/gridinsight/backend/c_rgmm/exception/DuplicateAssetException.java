package com.gridinsight.backend.c_rgmm.exception;

public class DuplicateAssetException extends RuntimeException {
    public DuplicateAssetException(String location, String identifier) {
        super("Asset already exists at location " + location + " with identifier " + identifier);
    }
}
