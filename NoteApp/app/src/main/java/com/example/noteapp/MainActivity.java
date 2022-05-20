package com.example.noteapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.noteapp.Adapters.NotesListAdapter;
import com.example.noteapp.Database.RoomDB;
import com.example.noteapp.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{


    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes= new ArrayList<>();
    RoomDB database ;
    FloatingActionButton fab_add;
    SearchView searchView_home;
    Notes seletedNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);
        database = RoomDB.getInstance(this);
        // đoạn này test;

        notes = database.mainDAO().getAll();

        updateRecycler(notes);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                startActivityForResult(intent,101);
            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });

    }

    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes note : notes
             ) {
            if(note.getTitle().toLowerCase().contains(newText.toLowerCase())
            || newText.toLowerCase().contains(note.getTitle().toLowerCase())){
                filteredList.add(note);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == 101){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                Toast.makeText(MainActivity.this, "Đã lưu thành công" , Toast.LENGTH_SHORT).show();
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();

            }
            else if (requestCode == 102){
                try {
                    Notes new_notes = (Notes) data.getSerializableExtra("note");
                    Toast.makeText(MainActivity.this, "" + new_notes.getID() + " " + new_notes.getTitle() , Toast.LENGTH_SHORT).show();
                    database.mainDAO().update(new_notes.getID(),new_notes.getTitle(), new_notes.getNotes());
                    notes.clear();
                    notes.addAll(database.mainDAO().getAll());
                    notesListAdapter.notifyDataSetChanged();
                }
                catch (Exception e){

                }
            }


    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this,notes,noteClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }
    private final NoteClickListener noteClickListener = new NoteClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this,NotesTakerActivity.class);
            intent.putExtra("old_note",notes);
            startActivityForResult(intent,102);

        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            seletedNotes = new Notes();
            seletedNotes = notes;
            showPopup(cardView);
        }
    };
    private void showPopup(CardView cardView){
        PopupMenu popupMenu =  new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.pin:
                if(seletedNotes.isPinned()){
                    database.mainDAO().pin(seletedNotes.getID(),false);
                    Toast.makeText(MainActivity.this, "Unpin", Toast.LENGTH_SHORT).show();
                }else{
                    database.mainDAO().pin(seletedNotes.getID(),true);
                    Toast.makeText(MainActivity.this, "Pinned", Toast.LENGTH_SHORT).show();
                }
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete:
                database.mainDAO().delete(seletedNotes);
                notes.remove(seletedNotes);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                notesListAdapter.notifyDataSetChanged();
                return true;
            default:
                return false;
        }

    }
}