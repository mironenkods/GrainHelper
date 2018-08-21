package com.edfman.grainhelper;

public class TypicalRef {
        String id_1c;
        String title;
        String driver_id;
        RefType type;

        public TypicalRef(String id_1c, String title, String driver_id, RefType type) {
            this.id_1c = id_1c;
            this.title = title;
            this.driver_id = driver_id;
            this.type = type;
        }

        public String getId_1c() {
            return id_1c;
        }

        public void setId_1c(String id_1c) {
            this.id_1c = id_1c;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDriver_id() {
            return driver_id;
        }

        public void setDriver_id(String driver_id) {
            this.driver_id = driver_id;
        }

        public RefType getType() {
            return type;
        }

        public void setType(RefType type) {
            this.type = type;
        }
    }

    enum RefType{car, driver, field, warehouse, crop}