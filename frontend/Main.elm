module Main exposing (main)

import Model exposing (Model)
import Messages exposing (Msg(..))
import Update exposing (update)
import View exposing (view)
import Subscriptions exposing (subscriptions)
import Requests exposing (getEvents)
import Navigation exposing (Location, program)
import Nav exposing (hashParser)


init : Location -> ( Model, Cmd Msg )
init location =
    ( Model [] [], getEvents )


main : Program Never Model Msg
main =
    program (ChangePage << hashParser)
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
