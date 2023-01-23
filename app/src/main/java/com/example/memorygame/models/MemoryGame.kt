package com.example.memorygame.models

import com.example.memorygame.utils.DEFAULT_ICONS

class MemoryGame (private val boardSize: BoardSize){

    val cards: List<MemoryCard>
    var numPairsFound = 0
    private var indexOfSingleSelectedCard : Int? = null
    private var numCardFlip = 0

    init {
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.numPairs())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int): Boolean {
        numCardFlip++
        val card = cards[position]
        var foundMatch = false
        // Two cases:
        // 0 cards flipped over => flip over the selected card + restore previous cards if != same
        // 1 card flipped over => flip over the selected card plus check if the cards matched
        if (indexOfSingleSelectedCard == null){
            //0 or 2 cards flipped
            restoreCards()
            indexOfSingleSelectedCard = position
        } else{
            // only one card flipped over
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
            }
        // Will change the position

        card.isFaceUp = !card.isFaceUp
        return foundMatch
        }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier){
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound ++
        return true
    }

    private fun restoreCards() {
        for (card in cards){

            if (!card.isMatched)
            {
                card.isFaceUp = false
            }

        }
    }

    fun haveWon(): Boolean {
        return (numPairsFound == boardSize.numPairs())

    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int{
        return numCardFlip/2
    }


}