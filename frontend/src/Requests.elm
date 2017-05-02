module Requests exposing (..)

import Messages exposing (Msg(..))
import Model.Event exposing (eventsDecoder)
import Model.Talk exposing (talkDecoder, talksDecoder)
import Http


getEvents : Cmd Msg
getEvents =
    Http.send GotEvents <|
        Http.get "http://localhost:8081/secured/data/events" eventsDecoder


getTalks : String -> Cmd Msg
getTalks id =
    Http.send GotTalks <|
        Http.get ("http://localhost:8081/secured/data/talks?eventId=" ++ id) talksDecoder


getTalk : String -> Cmd Msg
getTalk id =
    Http.send GotTalk <|
        Http.get ("http://localhost:8081/secured/data/atalk?talkId=" ++ id) talkDecoder
