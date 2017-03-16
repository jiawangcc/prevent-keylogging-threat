package com.example.jia.css539;

import java.util.ArrayList;
import java.util.List;

public class SensitiveInfoManager {
    private static final List<SensitiveInfoDefinition> sensitiveInfoDefinitionList;

    static {
        sensitiveInfoDefinitionList = new ArrayList<>(1);
        sensitiveInfoDefinitionList.add(new SensitiveInfoDefinition("abcd", 8));
        sensitiveInfoDefinitionList.add(new SensitiveInfoDefinition("1018", 10));
    }

    private static final SensitiveInfoManager INSTANCE = new SensitiveInfoManager();

    private final List<Character> charsSeen = new ArrayList<>();
    private int ptr = 0;
    private int ruleNumber = -1;
    private boolean prefixMatches = false;

    private SensitiveInfoManager() {}

    public static SensitiveInfoManager getInstance() {
        return INSTANCE;
    }

    public boolean matches(int key) {
        boolean encrypting = false;

        if (ruleNumber < 0) {
            for (int i = 0; i < sensitiveInfoDefinitionList.size(); i++) {
                if (ptr < sensitiveInfoDefinitionList.get(i).getPrefix().length() && Utils.toStr(key).equalsIgnoreCase(String.valueOf(sensitiveInfoDefinitionList.get(i).getPrefix().charAt(ptr)))) {
                    ruleNumber = i;
                    charsSeen.add(sensitiveInfoDefinitionList.get(i).getPrefix().charAt(ptr));
                    ptr++;

                    if (ptr == sensitiveInfoDefinitionList.get(i).getPrefix().length()) {
                        prefixMatches = true;
                    }
                }
            }
        }
        else {
            if (ptr < sensitiveInfoDefinitionList.get(ruleNumber).getPrefix().length() && Utils.toStr(key).equalsIgnoreCase(String.valueOf(sensitiveInfoDefinitionList.get(ruleNumber).getPrefix().charAt(ptr)))) {
                charsSeen.add(sensitiveInfoDefinitionList.get(ruleNumber).getPrefix().charAt(ptr));
                ptr++;

                if (ptr == sensitiveInfoDefinitionList.get(ruleNumber).getPrefix().length()) {
                    prefixMatches = true;
                }
            }else if (prefixMatches && ptr < sensitiveInfoDefinitionList.get(ruleNumber).getLength()) {
                encrypting = true;
                ptr++;
            }else{
                encrypting = false;
                prefixMatches = false;
                charsSeen.clear();
                ruleNumber = -1;
                ptr = 0;
                for (int i = 0; i < sensitiveInfoDefinitionList.size(); i++) {
                    if (Utils.toStr(key).equalsIgnoreCase(String.valueOf(sensitiveInfoDefinitionList.get(i).getPrefix().charAt(0)))) {
                        charsSeen.add(sensitiveInfoDefinitionList.get(i).getPrefix().charAt(0));
                        ruleNumber = i;
                        ptr = 1;
                    }
                }
            }
        }

        return encrypting;
    }
}
