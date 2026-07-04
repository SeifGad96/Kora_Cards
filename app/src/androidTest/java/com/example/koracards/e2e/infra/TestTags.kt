package com.example.koracards.e2e.infra

object TestTags {
    object Splash {
        const val SCREEN = "splash_screen"
        const val LOGO = "splash_logo"
        const val FOOTBALL = "splash_football"
    }
    object Home {
        const val SCREEN = "home_screen"
        const val HOST_BTN = "home_host_button"
        const val JOIN_BTN = "home_join_button"
        const val LANG_TOGGLE = "home_language_toggle"
        const val TITLE = "home_title"
        const val SETTINGS_BTN = "home_settings_button"
    }
    object HostWaiting {
        const val SCREEN = "host_waiting_screen"
        const val ROOM_CODE = "host_room_code"
        const val WAITING_INDICATOR = "host_waiting_indicator"
    }
    object JoinGame {
        const val SCREEN = "join_game_screen"
        const val ROOM_CODE_INPUT = "join_room_code_input"
        const val CONNECT_BTN = "join_connect_button"
        const val LOADING_INDICATOR = "join_loading_indicator"
    }
    object PlayerNameEntry {
        const val SCREEN = "player_name_entry_screen"
        const val NICKNAME_INPUT = "name_entry_nickname_input"
        const val CONFIRM_BTN = "name_entry_confirm_button"
        const val OPPONENT_STATUS = "name_entry_opponent_status"
    }
    object GameConfig {
        const val SCREEN = "game_config_screen"
        const val ROUNDS_STEPPER = "config_rounds_stepper"
        const val FAULTS_STEPPER = "config_faults_stepper"
        const val TIMER_TOGGLE = "config_timer_toggle"
        const val TIMER_SLIDER = "config_timer_slider"
        const val MCQ_TOGGLE = "config_mcq_toggle"
        const val MCQ_THRESHOLD = "config_mcq_threshold"
        const val START_GAME_BTN = "config_start_game_button"
    }
    object RoundStart {
        const val SCREEN = "round_start_screen"
        const val ROUND_BANNER = "round_start_banner"
        const val FIRST_PLAYER_INDICATOR = "round_start_first_player_indicator"
    }
    object Gameplay {
        const val SCREEN = "gameplay_screen"
        const val MY_CARD_SECTION = "gameplay_my_card_section"
        const val SCORE_DISPLAY = "gameplay_score_display"
        const val FAULT_COUNTER = "gameplay_fault_counter"
        const val TURN_INDICATOR = "gameplay_turn_indicator"
        const val TURN_TIMER = "gameplay_turn_timer"
        const val CORRECT_BTN = "gameplay_correct_button"
        const val WRONG_BTN = "gameplay_wrong_button"
        // Player Card Fields
        const val CARD_NAME = "card_player_name"
        const val CARD_AGE = "card_player_age"
        const val CARD_SHIRT = "card_player_shirt_number"
        const val CARD_NATIONALITY = "card_player_nationality"
        const val CARD_TEAM = "card_player_team"
        const val CARD_POSITION = "card_player_position"
        const val CARD_FOOT = "card_player_preferred_foot"
        const val CARD_LEAGUE = "card_player_league"
    }
    object MCQ {
        const val SCREEN = "mcq_screen"
        const val HEADER = "mcq_header"
        const val OPTION_A = "mcq_option_a"
        const val OPTION_B = "mcq_option_b"
        const val OPTION_C = "mcq_option_c"
        const val COUNTDOWN = "mcq_countdown"
        const val WAITING_INDICATOR = "mcq_waiting_indicator"
    }
    object CardReveal {
        const val SCREEN = "card_reveal_screen"
        const val CARD_FLIP = "card_reveal_flip_animation"
        const val REVEALED_CARD = "card_reveal_opponent_card"
    }
    object RoundResult {
        const val SCREEN = "round_result_screen"
        const val WINNER_BANNER = "round_result_winner_banner"
        const val SCORE_UPDATE = "round_result_score_update"
        const val NEXT_ROUND_BTN = "round_result_next_round_button"
    }
    object SuddenDeathIntro {
        const val SCREEN = "sudden_death_intro_screen"
        const val SD_BANNER = "sudden_death_banner"
    }
    object MatchWinner {
        const val SCREEN = "match_winner_screen"
        const val WINNER_TEXT = "match_winner_text"
        const val CONFETTI = "match_winner_confetti"
        const val FINAL_SCORE = "match_winner_final_score"
        const val PLAY_AGAIN_BTN = "match_winner_play_again_button"
        const val HOME_BTN = "match_winner_home_button"
    }
    object Settings {
        const val SCREEN = "settings_screen"
        const val LANG_TOGGLE = "settings_language_toggle"
        const val ABOUT_SECTION = "settings_about_section"
    }
    object ConnectionLost {
        const val SCREEN = "connection_lost_screen"
        const val COUNTDOWN = "connection_lost_countdown"
        const val CANCEL_BTN = "connection_lost_cancel_button"
    }
}
