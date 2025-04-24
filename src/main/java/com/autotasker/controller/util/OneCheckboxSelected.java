package com.autotasker.controller.util;

import javafx.scene.control.CheckBox;

public class OneCheckboxSelected {
    public static void ensureOnlyOneSelected(CheckBox first, CheckBox second) {
        second.selectedProperty()
                .addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        first.setSelected(false);
                    }
                });
        first.selectedProperty()
                .addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        second.setSelected(false);
                    }
                });
    }
}
