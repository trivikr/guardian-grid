import os
from shutil import copyfile
from . import *
import logging

LOGGER_NAME = os.path.splitext(os.path.basename(__file__))[0]
LOGGER = logging.getLogger(LOGGER_NAME)
OUTPUT_DIR = '/configs/etc/nginx'


def _get_mappings():
    domain = get_domain()
    mappings = get_mappings()

    for mapping in mappings:
        mapping['domain'] = domain

        # set default port
        mapping.setdefault('port', 9000)

    return mappings


def copy_nginx_conf():
    create_directory(OUTPUT_DIR)

    src = os.path.join(RESOURCE_PATH, 'nginx.conf')
    dest = os.path.join(OUTPUT_DIR, 'nginx.conf')

    LOGGER.info('creating {}'.format(dest))

    copyfile(src, dest)


def generate_sites_enabled():
    sites_enabled = os.path.join(OUTPUT_DIR, 'sites-enabled')

    create_directory(sites_enabled)

    mappings = _get_mappings()
    template = get_template_environment('nginx').get_template('https-proxy-server.template')

    for mapping in mappings:
        rendered = template.render(**mapping)
        output_path = os.path.join(sites_enabled, '{}.conf'.format(mapping.get('container')))
        LOGGER.info('creating {}'.format(output_path))

        with open(output_path, 'wb') as f:
            f.write(rendered)


def generate():
    copy_nginx_conf()
    generate_sites_enabled()

if __name__ == '__main__':
    LOGGER.info('Start')
    generate()
    LOGGER.info('End')
