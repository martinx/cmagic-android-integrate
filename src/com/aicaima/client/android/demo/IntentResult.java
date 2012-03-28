package com.aicaima.client.android.demo;

/**
 * 解码结果
 *
 * @author Martin Xu
 */

public final class IntentResult {
    private final String rawCode;
    private final String code;
    private final String itemId;
    private final String title;
    private final String content;

    private final String formatName;
    private final byte[] rawBytes;

    public IntentResult() {
        this(null, null, null, null, null, null, null);
    }

    public IntentResult(String rawCode, String code, String itemId, String title, String content, String formatName, byte[] rawBytes) {
        this.rawCode = rawCode;
        this.code = code;
        this.itemId = itemId;
        this.title = title;
        this.content = content;
        this.formatName = formatName;
        this.rawBytes = rawBytes;
    }

    public String getRawCode() {
        return rawCode;
    }

    public String getCode() {
        return code;
    }

    public String getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getFormatName() {
        return formatName;
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }
}
