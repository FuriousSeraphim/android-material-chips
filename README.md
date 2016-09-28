# Android Material Chips

A different approach to implement android material chips since using transformed images inside an EditText cause too many exceptions on older devices and older versions.

## Features
**Enter an email address and it will automatically transform into a chip**

<p>
<img src="./images/dc1.png" title="MaterialChips1" width="40%" />
<img src="./images/dc2.png" title="MaterialChips2" width="40%" />
</p>

**Customize your layout and text**
 
##Download

**Gradle:** 

via [jCenter](https://bintray.com/furiousseraphim/chips/materialchips/)
```gradle
buildscript {
    repositories {
        jcenter()
    }
}

dependencies {
    compile 'com.furiousseraphim.chips:material-chips:1.3.0'
}
```

##Usage

Use the ChipsView class in your layout file.

```xml
<com.seraphim.chips.ChipsView
    android:id="@+id/chipsView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

###Customize

**Layout**

Include ```xmlns:app="http://schemas.android.com/apk/res-auto"``` and customize your layout file.

```xml
<com.seraphim.chips.ChipsView
    android:id="@+id/chipsView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_max_height="120dp"
    app:cv_vertical_spacing="2dp"
    app:cv_bg_color="#f00"
    app:cv_bg_color_clicked="#0f0"
    app:cv_bg_color_error_clicked="#0f0"
    app:cv_color="#00f"
    app:cv_color_clicked="#721"
    app:cv_color_error_clicked="#f00"
    app:cv_text_color="#199"
    app:cv_text_color_clicked="#180"
    app:cv_text_color_error_clicked="#000"
    app:cv_icon_placeholder="@drawable/ic_bug_report_24dp"
    app:cv_icon_delete="@drawable/ic_close_24dp"
```
======
Find the View in your Activity or Fragment class.

```java
сhipsView = (ChipsView) findViewById(R.id.chipsView);
```

**Listener**

The ChipsView provides a listener to interact with your data.

```java
сhipsView.setChipsListener(new ChipsView.ChipsListener() {
    @Override
    public void onChipAdded(ChipsView.Chip chip) {
        // chip added
    }

    @Override
    public void onChipDeleted(ChipsView.Chip chip) {
        // chip deleted
    }

    @Override
    public void onTextChanged(CharSequence text) {
        // text was changed
    }
});
```

**Entry**

Implement ChipEntry
```java
public class MyChipEntry implements ChipEntry {
    private String name;
    private Uri imageUri;

    public MyChipEntry(String name, @Nullable Uri imageUri) {
        this.name = name;
        this.imageUri = imageUri;
    }

    @Override
    public String displayedName() {
        return name;
    }

    @Override
    public Uri avatarUri() {
        return imageUri;
    }
}
```
or use buit-in [SimpleChipEntry](https://github.com/FuriousSeraphim/android-material-chips/blob/master/library/src/main/java/com/seraphim/chips/SimpleChipEntry.java)


**Add a new chip**

```java
сhipsView.addChip(yourEntry);
```

**Add a non-removable chip.**

```java
сhipsView.addChip(yourEntry, true);
```

**Remove a chip**

```java
сhipsView.removeChipBy(entry);
```

**Add Custom chip validator**

```java
сhipsView.setChipsValidator(new ChipsView.ChipValidator() {
    @Override
    public boolean isValid(ChipEntry entry) {
        return true;
    }
});
```

**Add or set Suggestions**

```java
final List<ChipEntry> entries = new ArrayList<>();
// fill list
chipsView.setSuggestions(entries);
// or
chipsView.addSuggestions(entries);
```

**Set mode**

Only entries from suggestions list can be chips
```java
chipsView.setMode(ChipsView.Mode.ONLY_SUGGESTIONS);
```
or any inputted text can be chip(ability to set custom factory implementation coming soon)
```java
chipsView.setMode(ChipsView.Mode.ALL); // default
```

<p>
<img src="./images/chip_error.png" title="Chip Error" width="80%" />
</p>

## License
    Copyright (C) 2016 Doodle AG.
    Copyright 2016 FuriousSeraphim.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
