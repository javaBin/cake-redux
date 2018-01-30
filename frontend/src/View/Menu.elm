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
                , formatFilter
                , filterHeader "Length"
                , lengthFilter
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


formatFilter =
    fieldset []
        [ label []
            [ input [ type_ "checkbox" ] []
            , text "presentation"
            ]
        , label []
            [ input [ type_ "checkbox" ] []
            , text "workshop"
            ]
        , label []
            [ input [ type_ "checkbox" ] []
            , text "lightning-talk"
            ]
        ]


lengthFilter =
    fieldset []
        [ label []
            [ input [ type_ "checkbox" ] []
            , text "60 min"
            ]
        , label []
            [ input [ type_ "checkbox" ] []
            , text "45 min"
            ]
        , label []
            [ input [ type_ "checkbox" ] []
            , text "20 min"
            ]
        , label []
            [ input [ type_ "checkbox" ] []
            , text "10 min"
            ]
        ]
