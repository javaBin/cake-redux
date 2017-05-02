module Requests exposing (..)

import Messages exposing (Msg(..))
import Model.Event exposing (eventsDecoder)
import Model.Talk exposing (talksDecoder)
import Http


getEvents : Cmd Msg
getEvents =
    Http.send GotEvents <|
        Http.get "http://localhost:8081/secured/data/events" eventsDecoder


getTalks : String -> Cmd Msg
getTalks id =
    Http.send GotTalks <|
        Http.get ("http://localhost:8081/secured/data/talks?eventId=" ++ id) talksDecoder
