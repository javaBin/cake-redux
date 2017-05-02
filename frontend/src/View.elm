module View exposing (view)

import Model exposing (Model)
import Messages exposing (Msg)
import Model.Event exposing (Event)
import Model.Talk exposing (Talk)
import Model.Page exposing (Page(..))
import Nav exposing (toHash)
import Html exposing (Html, div, text, ul, li, a)
import Html.Attributes exposing (class, href)


view : Model -> Html Msg
view model =
    div [ class "app" ] <|
        [ div [ class "app__events" ] [ viewEvents model.events ]
        , div [ class "app__talks" ] [ viewTalks model.talks ]
        ]


viewEvents : List Event -> Html Msg
viewEvents events =
    ul [ class "events" ] <| List.map viewEvent events


viewEvent : Event -> Html Msg
viewEvent event =
    li [ class "events__event" ]
        [ a [ href <| toHash <| EventPage event.ref ] [ text event.name ] ]


viewTalks : List Talk -> Html Msg
viewTalks talks =
    ul [ class "talks" ] <| List.map viewTalk talks


viewTalk : Talk -> Html Msg
viewTalk talk =
    li [ class "talks__talk" ]
        [ text talk.title ]
