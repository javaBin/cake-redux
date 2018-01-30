module View exposing (view)

import Model exposing (Model)
import Messages exposing (Msg)
import Model.Event exposing (Event)
import Model.Talk exposing (Talk)
import Model.Page exposing (Page(..))
import View.Talk
import View.Menu
import Nav exposing (toHash)
import Html exposing (Html, div, text, ul, li, a, h2, h3, p)
import Html.Attributes exposing (class, href)


view : Model -> Html Msg
view model =
    div [ class "app" ] <|
        [ div [ class "app__menu" ] [ View.Menu.view model ]
        , div [ class "app__talks" ] [ viewTalks model ]
        , div [ class "app__talk" ] [ viewFullTalk model.talk ]
        ]


viewTalks : Model -> Html Msg
viewTalks model =
    ul [ class "talks" ] <| List.map (viewTalk <| Maybe.withDefault "" model.eventId) model.talks


viewTalk : String -> Talk -> Html Msg
viewTalk eventId talk =
    li [ class "talks__talk" ]
        [ a [ href <| toHash <| TalkPage eventId talk.ref ]
            [ div [ class "talks__talk__title" ] [ text talk.title ]
            , div [] [ text <| talk.format ++ " / " ++ talk.length ++ " minutes / " ++ talk.lang ]
            , div [] [ text <| "speakers: " ++ (String.join ", " <| List.map (\s -> s.name) talk.speakers) ]
            , div [] [ text <| "state: " ++ talk.state ]
            ]
        ]


viewFullTalk : Maybe Talk -> Html Msg
viewFullTalk maybeTalk =
    case maybeTalk of
        Nothing ->
            div [] []

        Just talk ->
            View.Talk.view talk
