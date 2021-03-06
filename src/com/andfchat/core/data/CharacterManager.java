/*******************************************************************************
 *     This file is part of AndFChat.
 *
 *     AndFChat is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AndFChat is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AndFChat.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.andfchat.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CharacterManager {

    @Inject
    private RelationManager relationManager;

    private final HashMap<String, FCharacter> knownCharacters = new HashMap<String, FCharacter>();

    private boolean statusChanged = false;

    // Username for system accounts
    public static final String USER_SYSTEM = "System";
    public static final String USER_SYSTEM_OUTPUT = "Output";
    public static final String USER_SYSTEM_INPUT = "Input";

    @Inject
    public CharacterManager() {
        clear();
    }

    public FCharacter findCharacter(String name, boolean create) {
        synchronized(this) {
            if (knownCharacters.containsKey(name) == false) {
                if (create) {
                    FCharacter character = new FCharacter(name);
                    relationManager.addRelationsToCharacter(character);
                    knownCharacters.put(name, character);
                }
            }
            return knownCharacters.get(name);
        }
    }

    public FCharacter findCharacter(String character) {
        return findCharacter(character, true);
    }

    public void addCharacter(FCharacter character) {
        relationManager.addRelationsToCharacter(character);

        synchronized(this) {
            if (knownCharacters.containsKey(character.getName()) == true) {
                knownCharacters.get(character.getName()).setInfos(character);
            } else {
                knownCharacters.put(character.getName(), character);
            }
        }
    }

    public void initCharacters(HashMap<String, FCharacter> characterList) {
        synchronized(this) {
            for (FCharacter character : characterList.values()) {
                relationManager.addRelationsToCharacter(character);
            }
            knownCharacters.putAll(characterList);
        }
    }

    public void removeCharacter(FCharacter character) {
        synchronized(this) {
            knownCharacters.remove(character.getName());
        }
    }

    public void clear() {
        knownCharacters.clear();
        // Reactivate standard user
        knownCharacters.put(USER_SYSTEM, new FCharacter(USER_SYSTEM));
        knownCharacters.put(USER_SYSTEM_OUTPUT, new FCharacter(USER_SYSTEM_OUTPUT, Gender.MALE));
        knownCharacters.put(USER_SYSTEM_INPUT, new FCharacter(USER_SYSTEM_INPUT, Gender.FEMALE));
    }

    public boolean isStatusChanged() {
        if (statusChanged) {
            statusChanged = false;
            return true;
        }

        return false;
    }

    public FCharacter changeStatus(String name, String status, String statusmsg) {
        statusChanged = true;

        FCharacter flistChar = findCharacter(name);
        flistChar.setStatus(status, statusmsg);

        return flistChar;
    }

    public List<FCharacter> getImportantCharacters() {
        List<FCharacter> importantCharacters = new ArrayList<FCharacter>();

        for (FCharacter character : knownCharacters.values()) {
            if (character.isImportant()) {
                importantCharacters.add(character);
            }
        }

        return importantCharacters;
    }
}
