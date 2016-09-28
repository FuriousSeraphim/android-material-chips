package com.doodle.android.chips.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seraphim.chips.ChipEntry;
import com.seraphim.chips.ChipsEntriesFilter;
import com.seraphim.chips.ChipsFactory;
import com.seraphim.chips.ChipsView;
import com.seraphim.chips.SimpleChipEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ChipsView chipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chipsView = (ChipsView) findViewById(R.id.cv_contacts);
        chipsView.setMode(ChipsView.Mode.NONE);

        final List<ChipEntry> entries = new ArrayList<>();
        entries.add(new SimpleChipEntry("example 1"));
        entries.add(new SimpleChipEntry("example 2", "http://www.topofandroid.com/wp-content/uploads/2015/05/Android-L-Material-Design-Wallpapers-5.png"));
        entries.add(new SimpleChipEntry("example 3", "https://appcyla.files.wordpress.com/2015/02/m1.jpg"));
        entries.add(new SimpleChipEntry("example 4", "http://geekhounds.com/wp-content/uploads/2014/11/unnamed.jpg"));
        entries.add(new SimpleChipEntry("example 5"));
        entries.add(new SimpleChipEntry("example 15"));
        entries.add(new SimpleChipEntry("example 6", "http://lifehacker.ru/wp-content/uploads/2014/11/14-14.png"));
        entries.add(new SimpleChipEntry("example 7"));
        entries.add(new SimpleChipEntry("example 11"));
//        chipsView.setSuggestions(entries);
        chipsView.addChips(entries);
//        chipsView.setChipsFilter(new CustomFilter());
//        chipsView.setFactory(new CustomFactory());
    }

    public class CustomFilter implements ChipsEntriesFilter {
        @Override
        public List<ChipEntry> filter(CharSequence constraint, List<ChipEntry> suggestions) {
            List<ChipEntry> newSuggs = new ArrayList<>();
            for (ChipEntry entry : suggestions) {
                if (entry.displayedName().contains("1")) newSuggs.add(entry);
            }
            return newSuggs;
        }
    }

    public class CustomFactory implements ChipsFactory {
        private final String[] wallpapers = {
                "https://static-s.aa-cdn.net/img/gp/20600003648579/sQDEOzjChlykXN1TnUXBAfIK2mhVvqaiImwHFkw3nPx-VXu6sA4CqSw0vz7NMK76QBM=w300?v=1",
                "http://lh6.ggpht.com/eAEhVDv1yUgdClKXNRQ5aHrrwDTaODndptQ0zOKWxscJ4OJA1iMSHqEzR2mKHMa8FBc=w256",
                "http://lh3.googleusercontent.com/IWQAvuRipnpNDJvsxJn6uSZelx0BCbqJA-RMDpsw1D7tkzneyK9fq1AM-SM3jFJixA=w256",
                "https://lh4.ggpht.com/hR29SvufnSNdBJrDa3xtqHXIfg3h8IH0Pv_4BfbDONn4om5RNHmnxJ4Pg8bYH7aTig=w256",
                "https://lh5.ggpht.com/HEETJTfAyCEhkUBkmEVMNht1WylVqyRVqu9eyE-ysSBSmeLzorJE_QebnKizB308eQ=w256",
                "https://static-s.aa-cdn.net/img/ios/987225753/7b0c3832d9cd33692534497841423178",
                "http://www.img.lirent.net/2014/10/Android-Lollipop-Material-Design-Wallpaper-3.png"
        };
        private final Random random = new Random(5);

        @Override
        public ChipEntry createChip(String text) {
            return new SimpleChipEntry(text, wallpapers[random.nextInt(wallpapers.length)]);
        }
    }
}