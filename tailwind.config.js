// tailwind.config.js
const {addDynamicIconSelectors} = require('@iconify/tailwind');
const mdi = require('@iconify-json/mdi').icons;

module.exports = {
    content: [
        './src/main/resources/templates/**/*.html',
        './src/main/resources/static/**/*.js',
        './src/main/resources/static/**/*.css',
    ],
    safelist: [
        {pattern: /^icon-\[mdi--/},
    ],
    theme: {
        extend: {},
    },
    plugins: [
        addDynamicIconSelectors({
            collections: {mdi},
        }),
    ],
}
