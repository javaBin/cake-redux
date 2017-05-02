module Main exposing (main)

import Model exposing (Model)
import Messages exposing (Msg)
import Update exposing (update)
import View exposing (view)
import Subscriptions exposing (subscriptions)
import Html exposing (Html, program, text, div)


init : ( Model, Cmd Msg )
init =
    ( Model "Hello, world", Cmd.none )


main : Program Never Model Msg
main =
    program
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
