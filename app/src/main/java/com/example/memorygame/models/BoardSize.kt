package com.example.memorygame.models

enum class BoardSize (val numCards: Int){

    EASY (numCards = 8),
    MEDIUM (numCards = 18),
    HARD (numCards = 24);

    fun getWidth(): Int {

        return when (this) {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 3
        }
    }
    fun getHeight(): Int {
        return numCards/getWidth()
    }

    fun numPairs(): Int {

        return numCards/2
    }
}