package com.oliverbotello.example.hms.wearengine.utils

enum class ProgressVars(val value: String) {
    ORIGEN_ANIMAL("prog_origenanimal"),
    VERDURAS("prog_verduras"),
    FRUTAS("prog_frutas"),
    LEGUMINOSAS("prog_leguminosas"),
    CEREALES("prog_cereales"),
    LASTEOS("prog_lacteos"),
    GRASAS("prog_grasas"),
    AZUCARES("prog_azucares"),
    AGUA("prog_agua"),
    SEMIDESCREMADA("prog_semidescremada"),
    CAFE("prog_cafe"),
    CAFE_AZUCAR("prog_cafeazucar"),
    REFRESCO_LIGHT("prog_refrescoligh"),
    JUGO("prog_jugo"),
    LECHE("prog_leche"),
    ENERGETICA("prog_energetica"),
    ALCOHOL("prog_alcohol"),
    REFRESCO("prog_refresco"),
    AGUA_SABOR("prog_aguasabor"),
    LECHE_AZUCAR("prog_lecheazucar"),
    SEMIAGUA("prog_semiagua"),
    SEMISEMIDESCREMADA("prog_semisemidescremada"),
    SEMICAFE("prog_semicafe"),
    SEMICAFE_AZUCAR("prog_semicafeazucar"),
    SEMIREFRESCO_LIGHT("prog_semirefrescoligh"),
    SEMIJUGO("prog_semijugo"),
    SEMILECHE("prog_semileche"),
    SEMIENERGETICA("prog_semienergetica"),
    SEMIALCOHOL("prog_semialcohol"),
    SEMIREFRESCO("prog_semirefresco"),
    SEMIAGUA_SABOR("prog_semiaguasabor"),
    SEMILECHE_AZUCAR("prog_semilecheazucar");

    companion object {
        fun getAllValues(): List<String> {
            return entries.map { it.value }
        }
    }
}