/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import type { PropsWithChildren } from "react";
import { Trans, useTranslation } from "react-i18next";

import { EInstanceFeature } from "@/models";

import { HintTooltip } from "../HintTooltip";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The feature the instance does not have.
     */
    feature: EInstanceFeature;
}

/**
 * Explains, on hover, why a sider entry is greyed out: the instance does not have the feature behind it.
 */
export const UnavailableFeatureTooltip = ({ feature, children }: PropsWithChildren<IProps>) => {
    const { t } = useTranslation();

    // Unlike the rest, the dependencies are never absent from an application, only from the starters older than 1.2
    const titleKey =
        feature === EInstanceFeature.DEPENDENCY_ANALYSIS
            ? "UnavailableFeature.notAvailableTitle"
            : "UnavailableFeature.notUsedTitle";

    return (
        <HintTooltip
            placement="right"
            content={
                <>
                    <span className={styles.Title}>{t(titleKey)}</span>
                    <span>
                        <Trans t={t} i18nKey={`UnavailableFeature.${feature}`} components={[<code key="0" />]} />
                    </span>
                </>
            }
        >
            <span className={styles.Trigger}>{children}</span>
        </HintTooltip>
    );
};
