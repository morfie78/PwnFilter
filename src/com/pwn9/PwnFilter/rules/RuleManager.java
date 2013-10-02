package com.pwn9.PwnFilter.rules;

import com.pwn9.PwnFilter.PwnFilter;

import java.io.File;
import java.util.HashMap;

/**
 * Manage RuleSets, rulefiles, etc.  All ruleChains that are to be managed by
 * PwnFilter are to be created / reloaded, etc. through this manager interface
 * User: ptoal
 * Date: 13-09-25
 * Time: 2:18 PM
 */

public class RuleManager {
    private static RuleManager _instance = null;
    private HashMap<String, RuleChain> ruleChains = new HashMap<String, RuleChain>();
    private File ruleDir;
    private PwnFilter plugin;


    private RuleManager(PwnFilter p) {
        plugin = p;
    }

    public static RuleManager getInstance() {

        if (_instance == null) {
            throw new IllegalStateException("Rule Manager not yet initialized!");
        } else {
            return _instance;
        }
    }

    public static RuleManager getInstance(PwnFilter p) {
        if (_instance == null) {
            _instance = new RuleManager(p);
        }
        return _instance;
    }

    public boolean setRuleDir(File dir) {
        if (dir.exists()) {
            ruleDir = dir;
            return true;
        } else return false;
    }

    /**
     * Get a File object pointing to the named configuration in the configured
     * Rule Directory.
     *
     * @param configName Name of configuration File to load
     * @return File object for requested config, or null if not found.
     */
    public File getFile(String configName) {
        if (ruleDir.exists()) {
            File ruleFile = new File(ruleDir,configName);
            if (ruleFile.exists()) {
                return ruleFile;
            } else {
                if (plugin.copyRuleTemplate(ruleFile, configName)) {
                    return ruleFile;
                } else {
                    return null;
                }
            }
        }
        PwnFilter.logger.warning("Unable to find or create rule file:" + configName);
        return null;
    }

    /*
     * Get a rulechain, or create a new one from the named config
     */
    public RuleChain getRuleChain(String configName) {
        RuleChain newRuleChain = new RuleChain(this, configName);
        ruleChains.put(configName,newRuleChain);
        return newRuleChain;
    }

    /*
     * Force all ruleChains to be refreshed.
     */
    public void reloadAllConfigs() {

        // We do this as a separate loop, to invalidate all ruleChains before
        // starting to reload the configs.  This ensures we check for cyclic
        // references properly.

        for (RuleChain rc : ruleChains.values()) {
            rc.resetChain();
        }

        // Now, reparse the configs
        for (String ruleSetName : ruleChains.keySet()) {
            RuleChain chain = ruleChains.get(ruleSetName);
            if (!chain.loadConfigFile()) {
                ruleChains.remove(ruleSetName);
            }
        }
    }

}