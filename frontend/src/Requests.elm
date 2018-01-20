module Requests exposing (..)

import Messages exposing (Msg(..))
import Model.Event exposing (eventsDecoder)
import Model.Talk exposing (Talk, talkDecoder, talksDecoder, talkEncoder)
import Json.Decode exposing (Decoder)
import Http exposing (Error, Request)


send : (Result Error a -> Messages.Msg) -> Request a -> Cmd Messages.Msg
send msg request =
    Http.send msg request
        |> Cmd.map
            (\m ->
                case m of
                    GotEvents (Err (Http.BadStatus res)) ->
                        checkStatus res m

                    GotTalks (Err (Http.BadStatus res)) ->
                        checkStatus res m

                    GotTalk (Err (Http.BadStatus res)) ->
                        checkStatus res m

                    TalkUpdated (Err (Http.BadStatus res)) ->
                        checkStatus res m

                    _ ->
                        m
            )


checkStatus : Http.Response body -> Messages.Msg -> Messages.Msg
checkStatus res msg =
    if res.status.code == 403 then
        Reauthenticate
    else
        msg


getEvents : String -> Cmd Msg
getEvents token =
    send GotEvents <|
        getRequest eventsDecoder token (url "events")


getTalks : String -> String -> Cmd Msg
getTalks id token =
    send GotTalks <|
        getRequest talksDecoder token (url <| "talks?eventId=" ++ id)


getTalk : String -> String -> Cmd Msg
getTalk id token =
    send GotTalk <|
        getRequest talkDecoder token (url <| "atalk?talkId=" ++ id)


updateTalk : Talk -> String -> Cmd Msg
updateTalk talk token =
    send TalkUpdated <|
        postRequest talkDecoder (Http.jsonBody <| talkEncoder talk) token (url "editTalk")


postRequest : Decoder a -> Http.Body -> String -> String -> Http.Request a
postRequest decoder body =
    request "POST" (Http.expectJson decoder) body


getRequest : Decoder a -> String -> String -> Http.Request a
getRequest decoder =
    request "GET" (Http.expectJson decoder) Http.emptyBody


request : String -> Http.Expect a -> Http.Body -> String -> String -> Http.Request a
request method expect body token url =
    Http.request
        { method = method
        , headers = [ Http.header "Accept" "application/json", authHeader token ]
        , url = url
        , body = body
        , expect = expect
        , timeout = Nothing
        , withCredentials = False
        }


url : String -> String
url path =
    "http://localhost:8081/api/secured/data/" ++ path


authHeader : String -> Http.Header
authHeader token =
    Http.header "Authorization" <| "Bearer " ++ token
