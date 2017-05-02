module Requests exposing (..)

import Messages exposing (Msg(..))
import Model.Event exposing (Event, eventsDecoder)
import Http


getEvents : Cmd Msg
getEvents =
    Http.send GotEvents <|
        Http.get "http://localhost:8081/secured/data/events" eventsDecoder
