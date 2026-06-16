package com.rx.admin.common.exception;

/**
 * 库不存在异常
 */
public class LibraryNotFoundException extends RuntimeException {

    private final String library;

    public LibraryNotFoundException(String library) {
        super("库 " + library + " 不存在");
        this.library = library;
    }

    public LibraryNotFoundException(String library, Throwable cause) {
        super("库 " + library + " 不存在", cause);
        this.library = library;
    }

    public String getLibrary() {
        return library;
    }
}
