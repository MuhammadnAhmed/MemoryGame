package com.example.memorygame

import android.animation.ArgbEvaluator
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard
import com.example.memorygame.models.MemoryGame
import com.example.memorygame.utils.DEFAULT_ICONS
import com.example.memorygame.utils.EXTRA_BOARD_SIZE
import com.google.android.material.snackbar.Snackbar


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object{
        private const val CREATE_REQUEST_CODE = 221
    }

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var movesText: TextView
    private lateinit var pairsText: TextView
    private lateinit var rvBoard: RecyclerView
    private lateinit var clRoot: ConstraintLayout
    private var boardSize : BoardSize = BoardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movesText = findViewById(R.id.textMoves)
        pairsText = findViewById(R.id.textPairs)
        rvBoard = findViewById(R.id.rvBoard)
        clRoot = findViewById(R.id.clRoot)

//        val intent = Intent(this, CreateActivity::class.java)
//        intent.putExtra(EXTRA_BOARD_SIZE, BoardSize.MEDIUM)
//        startActivity(intent)

        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh->{
                // Setup the game again
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWon()){
                    showAlertDialogue("Do you want to quit your game?", null, View.OnClickListener {
                        setupBoard()
                    })
                }
                else{
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size->{
                showNewSizeDialogue()
                return true
            }
            R.id.mi_custom_game->{
                showCreationDialogue()

            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialogue() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.layout_dialogue_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialogue("Create your own game", boardSizeView, View.OnClickListener {
            // set a new value fo board size
            val desiredBoardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rdEasy -> BoardSize.EASY
                R.id.rdMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // navigate to new activity
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialogue() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.layout_dialogue_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rdEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rdMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rdDifficult)
        }
        showAlertDialogue("Choose new size", boardSizeView, View.OnClickListener {
            // set a new value fo board size
            boardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rdEasy -> BoardSize.EASY
                R.id.rdMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showAlertDialogue(title: String, view: View?, positiveClickListener: View.OnClickListener ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ -> positiveClickListener.onClick(null) }.show()

    }

    private fun setupBoard() {
        when(boardSize){
            BoardSize.EASY -> {
                movesText.text = "Easy 4 x 2"
                pairsText.text = "Pairs: 0/4"
            }
            BoardSize.MEDIUM -> {
                movesText.text = "Easy 6 x 3"
                pairsText.text = "Pairs: 0/9"
            }
            BoardSize.HARD -> {
                movesText.text = "Easy 8 x 3"
                pairsText.text = "Pairs: 0/12"
            }
        }
        memoryGame = MemoryGame(boardSize)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClick(position: Int) {
                updateGameWithFlip(position)
            }
        } )
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {

        if (memoryGame.haveWon()){
            Snackbar.make(clRoot, "You already Won", Snackbar.LENGTH_LONG).show()
            return

        }
        if (memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot, "Invalid Move", Snackbar.LENGTH_SHORT).show()
            return
        }

        if(memoryGame.flipCard(position)){
            Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound}")

            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.numPairs(),
                ContextCompat.getColor(this, R.color.no_progress_color),
                ContextCompat.getColor(this, R.color.full_progress_color)
            ) as Int
            pairsText.setTextColor(color)
            pairsText.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.numPairs()}"
            if (memoryGame.numPairsFound == boardSize.numPairs()){
            Snackbar.make(clRoot, "You Won!", Snackbar.LENGTH_LONG).show()
            }
        }
        movesText.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}
