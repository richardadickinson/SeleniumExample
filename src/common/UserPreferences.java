/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

/**
 *
 * @author smaho01
 */

public class UserPreferences {
    private boolean removeEventsAfterAuditing;
    private boolean moveNextEventAfterAuditing;

    public UserPreferences(boolean removeEvents, boolean moveNextEvent) {
        removeEventsAfterAuditing = removeEvents;
        moveNextEventAfterAuditing = moveNextEvent;
    }

    public boolean removeEvents() {
        return removeEventsAfterAuditing;
    }

    public boolean moveNextEvent() {
        return moveNextEventAfterAuditing;
    }
}
