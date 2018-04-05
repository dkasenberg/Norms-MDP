/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.actions;

import burlap.mdp.core.action.Action;

/**
 * Corresponds to a action in the product MDP (= an action in the underlying MDP, plus settings corresponding to which
 * norms the agent decides to suspend/keep).
 *
 * @author dkasenberg
 */
public class CRDRAAction extends WrapperAction {

        public static final int MAINTAIN = 0;
        public static final int BREAKONCE = 1;
        public static final int GIVEUP = 2;

        public int[] settings;

        public CRDRAAction(Action action) {
            super(action);
            settings = new int[0];
        }

        public CRDRAAction(Action action, int numNorms) {
            super(action);
            settings = new int[numNorms];
        }
        
        public CRDRAAction(Action action, int[] normSettings) {
            super(action);
            settings = normSettings;
        }
        
        public int[] getSettings() {
            return settings;
        }
        
        public void setSettings(int[] settings) {
            this.settings = settings;
        }

        @Override
        public Action copy() {
            return new CRDRAAction(this.action.copy(), settings.clone());
        }

    @Override
    public String toString() {
        return toStringWithSettings();
    }

    private String toStringWithSettings() {
            String toReturn = action.toString() + "(";
            
            String[] settingsString = new String[settings.length];
            for(int i = 0; i < settings.length; i++) {
                switch(settings[i]) {
                    case MAINTAIN:
                        settingsString[i] = "m";
                        break;
                    case BREAKONCE:
                        settingsString[i] = "b";
                        break;
                    case GIVEUP:
                        settingsString[i] = "g";
                        break;
                }
            }
                        
            return super.toString() + " (" + String.join(",", settingsString) + ")";
        }

        @Override
        public boolean equals(Object other) {

            if(!(other instanceof CRDRAAction)) {
                if(other instanceof WrapperAction) {
                    return action.equals(((WrapperAction)other).action);
                }
                return action.equals(other);
            }

            CRDRAAction otherCRDRA = (CRDRAAction)other;

            int[] otherSettings = otherCRDRA.settings;
            if(otherSettings == null || otherSettings.length != this.settings.length) {
                return false;
            }

            for(int i = 0; i < settings.length; i++) {
                if(settings[i] != otherSettings[i]) return false;
            }

            return action.equals(otherCRDRA.action);
        }
    }