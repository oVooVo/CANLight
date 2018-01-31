package com.example.pascal.canlight.chordPattern.UltimateGuitarChordPatternImporter;

import com.example.pascal.canlight.chordPattern.ChordPatternImporter;

/**
 * Created by pascal on 1/19/18.
 */

public abstract class UltimateGuitarChordPatternImporter extends ChordPatternImporter {
    public static UltimateGuitarChordPatternImporter getMostCurrentInstance() {
        return new UltimateGuitarChordPatternImporter_v0();
    }
}
