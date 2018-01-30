module View.Menu exposing (view)

import Model.Event exposing (Event)
import Messages exposing (Msg(..))
import Model.Page exposing (Page(..))
import Nav exposing (toHash)
import Model exposing (Model)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)


view : Model -> Html Msg
view model =
    div [ class "menu" ]
        [ div [ class "menu__block" ]
            [ div [ class "menu__header" ] [ text "Choose event..." ]
            , div [] [ select [ class "menu__selector", onInput (changePage) ] <| List.map createOption model.events ]
            ]
        , div [ class "menu__block" ]
            [ div [ class "menu__header" ] [ text "Filters" ]
            , div []
                [ div [ class "menu__filter" ] [ text "Format" ]
                , div [] [ text "todo..." ]
                ]
            ]
        ]


changePage : String -> Msg
changePage page =
    ChangePage <| EventPage page


createOption : Event -> Html Msg
createOption event =
    option [ value event.ref ] [ text event.name ]
