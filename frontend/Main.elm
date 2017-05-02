module Main exposing (main)

import Model exposing (Model)
import Messages exposing (Msg)
import Update exposing (update)
import View exposing (view)
import Subscriptions exposing (subscriptions)
import Requests exposing (getEvents)
import Html exposing (Html, program, text, div)


init : ( Model, Cmd Msg )
init =
    ( Model [], getEvents )


main : Program Never Model Msg
main =
    program
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
