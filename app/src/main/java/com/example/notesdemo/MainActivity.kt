package com.example.notesdemo

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesdemo.adapters.NotesListAdapter
import com.example.notesdemo.model.Notes
import com.example.notesdemo.veiwmodel.NotesViewModel
import com.example.notesdemo.veiwmodel.NotesViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
// FIXME видно неиспользуемый импорт - надо использовать автоматическое удаление неиспользуемых импортов
//  фича IDE - Optimize imports
import com.google.android.material.internal.ContextUtils.getActivity
// FIXME нужно убрать использование синтетиков, их развитие остановлено и в любой момент они могут
//  вообще перестать работать.
//  Равноценная замена будет - https://developer.android.com/topic/libraries/view-binding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notes_list_item.*

class MainActivity : AppCompatActivity() {

    // FIXME не надо сохранять ссылки на UI элементы, это места для потенциальных ошибок с утечкой памяти
    //  по сути элементы эти нужны только в момент настройки, поэтому сохранять их на долго нет нужды
    private lateinit var deleteItem: MenuItem
    private lateinit var searchItem: MenuItem

    // FIXME из-за того что выбор enabled/disabled просто свойство активити мы потеряем это значение
    //  после пересоздания активити. а это произойдет обязательно.
    //  стоит почитать блок в андроид доке - https://developer.android.com/guide/topics/resources/runtime-changes?hl=en
    //  рекомендация моя - унести состояние в ViewModel
    private var selectionModeEnabled = false
    // FIXME это свойство точно не должно быть публичным. никто снаружи этого класса не должен знать
    //  об этой детали реализации. А также ссылка на adapter нужна нам только в течении создания view
    //  в onCreate, и только там нам надо создать адаптер, прицепить его к recyclerView и указать
    //  логику заполнения данных
    val adapter = NotesListAdapter()

    // FIXME это свойство тоже не должно быть публичным. это детали реализации конкретно этого активити
    val notesVModel: NotesViewModel by viewModels {
        // FIXME тут надо получение зависимостей переделать, по однмоу из вариантов описанных в NotesApplication
        NotesViewModelFactory((application as NotesApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_activity_toolbar)

        recyclerview_notes.layoutManager = LinearLayoutManager(this)
        recyclerview_notes.adapter = adapter

        adapter.setOnNoteTapListener { note ->
            // FIXME как я уже говорил выше лучше логику выбора вынести в viewmodel.
            //  и тогда тут у тебя будет просто вызов обработчика вьюмодели onNotePressed
            //  а уже viewModel будет определять надо ли добавить новую заметку в выбранные,
            //  или же надо пойти на другой экран. В случае если надо пойти на другой экран просто
            //  пульнет в Flow с событиями соответствующее событие `OpenNote(noteId)`
            //  https://proandroiddev.com/android-singleliveevent-redux-with-kotlin-flow-b755c70bb055
            if (!selectionModeEnabled) {
                // FIXME логика формирования опций для открытия экрана должна быть вынесена в
                //  отдельную функцию, что позволит реюзать эти опции
                val bundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions.makeSceneTransitionAnimation(this,iv_has_image,iv_has_image.transitionName).toBundle()
                } else {
                    ActivityOptionsCompat.makeCustomAnimation(this, 0, 0).toBundle()
                }
                // FIXME вся логика формирования правильного Intent должна быть не в месте открытия
                //  активити, а в companion object в открываемой активити. пример там оформил
                //  createIntent(context: Context, note: Notes): Intent
                //  таким образом мы даем возможность легко создавать интент для открытия экрана
                //  откуда угодно, не дублируя код и не показывая внутрянку активити другим классам
                val intent = Intent(this@MainActivity, EditNote::class.java)
                // FIXME передавать между экранами прям объект заметки - оверхед.
                //  лучше передавать id заметки, а целевой экран без пробелм считает с базы данных
                //  эту заметку по ее id
                intent.putExtra("note", note)
                // FIXME IDE подсказывает что такой метод недоступен на API 14, которая заявлена как
                //  поддерживаемая
                startActivity(intent,bundle)
            } else {
                startSelection(note)
            }
        }

        adapter.onNoteLongClickListener { note ->
            // FIXME обработкой лонг клика тоже заниматься вьюмодели стоит. она включит режим выбора,
            //  добавит в выбранные эелменты заметку которую жмем
            selectionModeEnabled = true
            startSelection(note)
        }


        notesVModel.allNotes.observe(this) { notes ->
            notes.let { adapter.setNotes(it) }
        }

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        with(recyclerview_notes) {
            // FIXME это уже применено, зачем повторно?
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(divider)
        }

        // FIXME когда подключишь ViewBinding надо заменить на типобезопасное получение вьюхи
        val fab = findViewById<FloatingActionButton>(R.id.fab_main_add_note)
        fab.setOnClickListener {
            // FIXME также как я выше говорил - надо чтобы формирование интента было не тут а
            //  в companion object'е активити которую открываем. и никаких хардкод строк в ключах
            //  интента не должно быть
            val intent = Intent(this@MainActivity, EditNote::class.java)
            intent.putExtra("isEdit",true)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        // FIXME вместо сохранения в свойства класса активити этих элементов - надо просто здась
        //  получить эти элементы и подписаться на изменение лайвдаты в viewModel, в зависимости от
        //  которой скрывать или показывать эти элементы
        deleteItem = menu.findItem(R.id.action_delete)
        searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        // FIXME никогда нигде не должно быть хардкод строк которые выводятся юзеру. Все выводимые
        //  юзеру строки должны браться из ресурсов андроида, для поддержки локализации.
        searchView.queryHint = "Введите наименование для поиска"
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                notesVModel.handleSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                notesVModel.handleSearchQuery(newText)
                return true
            }
        })
        return true
    }

    // FIXME эта пачка логики должна уйти в вьюмодель и по сути делать следующее:
    //  лайвдата selectionMode включается в true, в лайвдату selectedNotes добавляем id выбранного наим элемента
    //  а уже на основе этих лайвдат у нас должен перестраиваться UI, за счет поставленных в onCreate observe
    private fun startSelection(note: Notes) {
        note.selected = !note.selected
        val index = adapter.notesList.indexOf(note)
        adapter.notifyItemChanged(index)
        if (adapter.notesList.none { noteSel ->
                noteSel.selected
            }) {
            setDefaultToolbar()
            selectionModeEnabled = false
        } else {
            setDeleteToolBar()
        }
    }

    // FIXME должно выставляться это на основании подписки на лайвдату selectedMode из вьюмодели
    private fun setDefaultToolbar() {
        title = resources.getString(R.string.app_name)
        deleteItem.isVisible = false
        searchItem.isVisible = true
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }

    // FIXME должно выставляться это на основании подписки на лайвдату selectedMode из вьюмодели
    private fun setDeleteToolBar() {
        title = adapter.getSelectedList().size.toString()
        deleteItem.isVisible = true
        searchItem.isVisible = false
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    // FIXME это логика вьюмодели уже, она должна менять данные а UI просто обновится после
    //  получения новой инфы
    private fun deselectAllNotes() {
        adapter.notesList.forEachIndexed { position, note ->
            if (note.selected) {
                val index = adapter.notesList.indexOf(note)
                note.selected = false
                adapter.notifyItemChanged(index)
            }
        }
        setDefaultToolbar()
        selectionModeEnabled = false
    }

    // FIXME это должно быть логикой вьюмодели. она удаляет из репозитория, она обновляет лайвдату,
    //  а UI реагирует только на изменения livedata
    private fun deleteSelectedNotes() {
        val selList = adapter.getSelectedList()
        if (selList.isNotEmpty()) {
            val firstSelItem = selList[0]
            val position = adapter.notesList.indexOf(firstSelItem)
            deleteNote(firstSelItem, position)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            deselectAllNotes()
            true
        }
        R.id.action_delete -> {
            deleteSelectedNotes()
            true
        }
        else ->
            super.onOptionsItemSelected(item)
    }

    // FIXME логика удаления вся во вьюмодели должна быть. не должен ui чето там додумывать -
    //  он просто обновляется реагируя на изменения livedata
    private fun deleteNote(note: Notes, position: Int) {
        val isNoteDeleted = notesVModel.delete(note)
        isNoteDeleted.also { /*dataOrException ->
            if (isProductDeleted != null) {*/
            adapter.notesList.remove(note)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, adapter.notesList.size)
            if (adapter.getSelectedList().isEmpty()) {
                setDefaultToolbar()
                selectionModeEnabled = false
            }
            deleteSelectedNotes()
            //}

        }
    }
}