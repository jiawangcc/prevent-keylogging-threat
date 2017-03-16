package com.example.jia.css539;

import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class InterceptIME implements IXposedHookLoadPackage {

    private static final boolean VERBOSE = true;

    private final byte[] key = "EncryptionKey".getBytes();
    private final RC4 rc4 = new RC4(key);

    private boolean encrypting = false;
    private boolean sensitiveContext = false;


    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {

        findAndHookMethod("com.example.android.softkeyboard.SoftKeyboard", loadPackageParam.classLoader, "onKey", int.class, int[].class, new XC_MethodHook() {
            long timeStart;
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                timeStart = System.nanoTime();
                EditorInfo editorInfo = ((InputMethodService) param.thisObject).getCurrentInputEditorInfo();
                XposedBridge.log("[InputMethodInterceptor] editorInfo.inputType:" + editorInfo.inputType);
                if (VERBOSE) {
                    XposedBridge.log("[InputMethodInterceptor] invoked before onKey() keyCode:" + Utils.toStr((int) param.args[0]));
                }
                sensitiveContext = (editorInfo.inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD;
                if (!sensitiveContext) {
                    encrypting = SensitiveInfoManager.getInstance().matches((int)param.args[0]);
                    if (encrypting) {
                        param.args[0] = rc4.encrypt((int)param.args[0]);
                    }
                } else {
                    param.args[0] = rc4.encrypt((int)param.args[0]);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("[InputMethodInterceptor] time (ns) taken in onKey():" + String.valueOf(System.nanoTime() - timeStart));
            }
        });

        findAndHookMethod("com.example.android.softkeyboard.SoftKeyboard", loadPackageParam.classLoader, "onUpdateSelection", int.class, int.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
            long timeStart;
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                timeStart = System.nanoTime();
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!sensitiveContext && !encrypting) {
                    // do nothing
                } else {
                    InputConnection inputConnection = ((InputMethodService) param.thisObject).getCurrentInputConnection();
                    CharSequence extractedText = extractText(inputConnection);
                    if (VERBOSE) {
                        XposedBridge.log("[InputMethodInterceptor] invoked after getCurrentInputConnection() extractedText:" + extractedText);
                    }
                    int oldSelEnd = (int) param.args[1];
                    int newSelEnd = (int) param.args[3];
                    if (extractedText.length() > 0 && (newSelEnd - oldSelEnd == 1)) {
                        inputConnection.setComposingRegion(oldSelEnd, newSelEnd);
                        int keyevent = (int) extractedText.charAt(oldSelEnd);
                        int[] cipherText = {keyevent};
                        inputConnection.commitText(String.valueOf((char) rc4.decrypt(cipherText)[0]), 1);
                    }
                    if (VERBOSE) {
                        XposedBridge.log("[InputMethodInterceptor] invoked after getCurrentInputConnection() commitText restoredText:" + extractText(inputConnection));
                    }
                }
                XposedBridge.log("[InputMethodInterceptor] time (ns) taken in onUpdateSelection():" + String.valueOf(System.nanoTime() - timeStart));
            }
        });
    }

    private CharSequence extractText(InputConnection inputConnection) {
        ExtractedTextRequest request = new ExtractedTextRequest();
        request.token = 0;
        request.flags = InputConnection.GET_EXTRACTED_TEXT_MONITOR;
        request.hintMaxChars = 10000;
        request.hintMaxLines = 10;
        return inputConnection.getExtractedText(request, InputConnection.GET_EXTRACTED_TEXT_MONITOR).text;
    }
}
