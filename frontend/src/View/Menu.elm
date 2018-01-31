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
            [ div [ class "menu__header" ] [ text "Filters (TODO)" ]
            , div []
                [ filterHeader "Search"
                , input [ value "search for anything here " ] []
                , filterHeader "Format"
                , formatFilter model.filterData.formats
                , filterHeader "Length"
                , lengthFilter model.filterData.lengths
                ]
            ]
        ]


changePage : String -> Msg
changePage page =
    ChangePage <| EventPage page


createOption : Event -> Html Msg
createOption event =
    option [ value event.ref ] [ text event.name ]


filterHeader : String -> Html Msg
filterHeader header =
    div [ class "menu__filter" ] [ text header ]


formatFilter : List String -> Html Msg
formatFilter formats =
    fieldset [] <| List.map checkbox formats


lengthFilter : List String -> Html Msg
lengthFilter lengths =
    fieldset [] <| List.map checkbox lengths


checkbox txt =
    label []
        [ input [ type_ "checkbox" ] []
        , text txt
        ]
