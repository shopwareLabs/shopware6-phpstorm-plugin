import template from './sw-button.html.twig';

const { Component } = Shopware;

Component.register('sw-button', {
    template,

    props: {
        disabled: {
            type: Boolean,
            required: false,
            default: false,
        },
        variant: {
            type: String,
            required: false,
            default: '',
            validValues: ['primary', 'ghost', 'danger', 'ghost-danger', 'contrast', 'context'],
            validator(value) {
                if (!value.length) {
                    return true;
                }
                return ['primary', 'ghost', 'danger', 'ghost-danger', 'contrast', 'context'].includes(value);
            },
        },
        size: {
            type: String,
            required: false,
            default: '',
            validValues: ['x-small', 'small', 'large'],
            validator(value) {
                if (!value.length) {
                    return true;
                }
                return ['x-small', 'small', 'large'].includes(value);
            },
        },
    },
});