module Requests exposing (..)

import Messages exposing (Msg(..))
import Model.Event exposing (eventsDecoder)
import Model.Talk exposing (Talk, talkDecoder, talksDecoder, talkEncoder)
import Http


getEvents : Cmd Msg
getEvents =
    Http.send GotEvents <|
        Http.get (url "events") eventsDecoder


getTalks : String -> Cmd Msg
getTalks id =
    Http.send GotTalks <|
        Http.get (url <| "talks?eventId=" ++ id) talksDecoder


getTalk : String -> Cmd Msg
getTalk id =
    Http.send GotTalk <|
        Http.get (url <| "atalk?talkId=" ++ id) talkDecoder


updateTalk : Talk -> Cmd Msg
updateTalk talk =
    Http.send TalkUpdated <|
        Http.post (url "editTalk") (Http.jsonBody <| talkEncoder talk) talkDecoder


url : String -> String
url path =
    "http://localhost:8081/secured/data/" ++ path
