module View.Menu exposing (view)

import Model.Event exposing (Event)
import Messages exposing (Msg)
import Model.Page exposing (Page(..))
import Nav exposing (toHash)
import Model exposing (Model)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)


view : Model -> Html Msg
view model =
    div []
        [ div [ class "menu__header" ] [ text "Choose event..." ]
        , ul [ class "events" ] <| List.map viewEvent model.events
        , div [ class "menu__header" ] [ text "Filters" ]
        , div [] [ text "todo..." ]
        ]


viewEvent : Event -> Html Msg
viewEvent event =
    li [ class "events__event" ]
        [ a [ href <| toHash <| EventPage event.ref ] [ text event.name ] ]
