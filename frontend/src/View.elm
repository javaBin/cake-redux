module View exposing (view)

import Model exposing (Model)
import Messages exposing (Msg)
import Model.Event exposing (Event)
import Html exposing (Html, div, text, ul, li)
import Html.Attributes exposing (class)


view : Model -> Html Msg
view model =
    viewEvents model.events


viewEvents : List Event -> Html Msg
viewEvents events =
    ul [ class "events" ] <| List.map viewEvent events


viewEvent : Event -> Html Msg
viewEvent event =
    li [ class "events__event" ] [ text event.name ]
